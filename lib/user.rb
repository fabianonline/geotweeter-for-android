class User
	include DataMapper::Resource

	property :id, Serial
	property :twitter_id, String, :required=>true, :length=>100
	property :twitter_token, String, :required=>true, :length=>100
	property :twitter_secret, String, :required=>true, :length=>100
	property :twitter_screen_name, String, :required=>true, :length=>32

	has n, :reg_ids

	validates_uniqueness_of :twitter_id

	before :destroy do
		reg_ids.all.destroy()
	end

	@@instances = {}

	def self.instances; @@instances; end

	def start_stream
		@@instances[self.twitter_id] = self

		log "Starting stream."
		opts = {
			:path=>"/1.1/user.json",
			:host=>"userstream.twitter.com",
			:oauth=>{
				:consumer_key=>$CONSUMER_TOKEN,
				:consumer_secret=>$CONSUMER_SECRET,
				:token=>self.twitter_token,
				:token_secret=>self.twitter_secret
			}
		}
		last_reconnect = Time.now
		
		machine = EM.run do
			client = EM::Twitter::Client.connect(opts)
			unauthorized_count = 0
			
			client.each do |result|
				begin
					Stats.add_bytes(result.length)
					data = JSON.parse(result)
					if data.has_key?("direct_message")
						data = data["direct_message"]
					end
					if data.has_key?("text") && data.has_key?("recipient") && data["recipient"]["id_str"]==self.twitter_id
						log "DM."
						send_message(data, "dm")
						Stats.add_dm()
					elsif data.has_key?("text")
						if data["entities"]["user_mentions"].any?{|mention| mention["id_str"]==self.twitter_id}
							log "Mention. #{data["text"]}"
							send_message(data, "mention")
							Stats.add_mention()
						elsif data.has_key?("rewteeted_status") && data["retweeted_status"]["user"]["id_str"]==self.twitter_id
							log "Retweet"
							send_message(data, "retweet")
							Stats.add_retweet()
						else
							Stats.add_normal_tweet()
						end
					elsif data.has_key?("event") && data["event"]=="favorite" && data["source"]["id_str"]!=self.twitter_id
						log "Favorited"
						send_message(data, "favorite")
						Stats.add_favorite()
					end
				rescue Exception=>e
					log "EXCEPTION: #{e}"
					e.backtrace.each {|line| log "    #{line}"}
				end
			end
			
			client.on_forbidden { log "Forbidden. o_O" }
			client.on_unauthorized do
				unauthorized_count += 1
				log "Unauthorized. o_O ##{unauthorized_count}"
				if unauthorized_count > 15
					log "Too many unauthorized messages. Stopping and deleting account."
					client.connection.stop
					self.destroy!()
				end
			end
			client.on_reconnect do
				log "Reconnect."
				last_reconnect = Time.now
				Stats.add_reconnect()
			end

			client.on_close do
				if (Time.now - last_reconnect) > 1
					log "Closed."
				end
			end
			client.on_not_found { log "Not found. o_O" }
			client.on_not_acceptable { log "Not acceptable. o_O" }
			client.on_too_long { log "Too long. o_O" }
			client.on_range_unacceptable { log "Range unacceptable. o_O" }
			client.on_rate_limited { log "Rate limited. o_O"; sleep 300 }
		end
		Stats.add_account()
		Stats.add_reg_id(self.reg_ids.count)
	end

	def send_message(data, type)
		reg_ids = {}
		self.reg_ids.reload.all.each do |r|
			reg_ids[r.version] ||= []
			reg_ids[r.version] << r.reg_id
		end
		if reg_ids[0] && reg_ids[0].count > 0
			# send GCM for devices using version 0
			data = {:type=>type, :data=>data.to_json, :user_id=>self.twitter_id, :version=>0}
			send_gcm(data, reg_ids[0])
		end
		if reg_ids[1] && reg_ids[1].count > 0
			# send GCM for devices using version 1
			data = {:type=>type, :data=>Base64.encode64(Zlib::Deflate.deflate(data.to_json)), :user_id=>self.twitter_id, :version=>1}
			send_gcm(data, reg_ids[1])
		end
	end

	def send_gcm(data, reg_ids)
		result = $gcm_sender.send(reg_ids, {:data=>data})
		result = JSON.parse(result.body)
		if result["success"] < reg_ids.count
			log "ERROR! Success was #{result['success']}, expected #{config[:reg_ids][version].count}. Error handling is still missing here!"
			
			# not_registered_reg_ids = []
			# result['results'].each_with_index do |res, id|
			# 	if res['error']=="NotRegistered"
			# 		not_registered_reg_ids << config[:reg_ids][version][id]
			# 	end
			# end
			
			# not_registered_reg_ids.each do |reg_id|
			# 	config[:reg_ids][version].delete reg_id
			# 	log "ERROR: Not Registered Device - removing."
			# 	$stats[:reg_ids] -= 1
			# end
			
			# save_settings unless not_registered_reg_ids.empty?
			
			# unless result["success"]==(config[:reg_ids][version].count - not_registered_reg_ids.count)
			# 	log "ERROR: #{result.inspect}"
			# 	log "ERROR: Sent data packet length was: #{data.length}"
			# end
		end
		Stats.add_successful_gcms(result['success'].to_i)
		Stats.add_failed_gcms(result['failure'].to_i)
	end

	def log(string); puts "%s   %-20s   %s" % [Time.now, self.twitter_screen_name, string]; end
end