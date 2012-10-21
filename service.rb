#!/usr/bin/ruby

require 'rubygems'
require 'bundler'
require 'digest/sha2'
Bundler.require

Thread.abort_on_exception = true
$settings = YAML::load_file("settings.yml") rescue {}
$digest = Digest::SHA256.new
$stats = {:normal_tweets=>0, :mentions=>0, :dms=>0, :bytes=>0, :accounts=>0, :reg_ids=>0, :gcms_successful=>0, :gcms_failed=>0, :favorites=>0, :retweets=>0, :re_registrations=>0, :reconnects=>0}

$properties = {}
IO.readlines(File.join(File.dirname(__FILE__), "geotweeter.properties")).each do |l|
	parts=l.split("=", 2)
	next unless parts[1]
	$properties[parts[0]] = parts[1].strip
end


$gcm_sender = HiGCM::Sender.new($properties['google.gcm.sender.token'])
CONSUMER_TOKEN  = $properties['twitter.consumer.key']
CONSUMER_SECRET = $properties['twitter.consumer.secret']

def update()
	(lines = IO.readlines("command.txt")) rescue return
	lines.each do |line|
		command, token, secret, reg_id, screen_name = *line.split(" ")
		(id = token.split("-")[0].to_i) rescue next
		if command=="add"
			next unless reg_id && reg_id.length>5
			if $settings.has_key? id
				$settings[id][:screen_name] = screen_name
				$settings[id][:token] = token
				$settings[id][:secret] = secret
				if $settings[id].has_key?(:reg_ids) && $settings[id][:reg_ids].include?(reg_id)
					log screen_name, "Not adding client: Already known."
					$stats[:re_registrations] += 1
					next
				end
				$settings[id][:reg_ids] << reg_id
				log(screen_name, "Adding new reg_id")
				$stats[:reg_ids] += 1
			else
				hash = {:token=>token, :secret=>secret, :reg_ids=>[reg_id], :screen_name=>screen_name, :user_id=>id}
				$settings[id] = hash
				log(screen_name, "Adding stream")
				stream(id)
			end
		elsif command=="del"
			next unless $settings.has_key? id
			$stats[:reg_ids] -= 1 if $settings[id][:reg_ids].delete(reg_id)
			if $settings[id][:reg_ids].empty?
				$settings.delete id
				log(screen_name, "Stream fhas no more reg_ids left.")
				$settings[id][:client].connection.stop if $settings[id][:client]
				$stats[:accounts] -= 1
			else
				log(screen_name, "Removed reg_id from #{screen_name}")
			end
		end
	end
	save_settings
	FileUtils.rm(File.join(File.dirname(__FILE__), "command.txt"))
end

def save_settings
	clone = $settings.clone
	copy = {}
	clone.each do |key, hash|
		hash.delete :client
		copy[key] = hash
	end
	File.open(File.join(File.dirname(__FILE__), "settings.yml"), "w") {|f| f.write YAML.dump(copy)}
end

def send_gcm(config, data, type)
	result = $gcm_sender.send(config[:reg_ids], {:data=>{:type=>type, :data=>data.to_json, :user_id=>config[:user_id]}})
	result = JSON.parse(result.body)
	unless result["success"]==config[:reg_ids].count
		log "ERROR! Success was #{result['success']}, expected #{config[:reg_ids].count}"
		not_registered_reg_ids = []
		result['results'].each_with_index do |res, id|
			if res['error']=="NotRegistered"
				not_registered_reg_ids << config[:reg_ids][id]
			end
		end
		
		not_registered_reg_ids.each do |reg_id|
			config[:reg_ids].delete reg_id
			log "ERROR: Not Registered Device - removing."
			$stats[:reg_ids] -= 1
		end
		
		save_settings unless not_registered_reg_ids.empty?
		
		unless result["success"]==(config[:reg_ids].count - not_registered_reg_ids.count)
			log "ERROR: #{result.inspect}"
			log "ERROR: Sent data packet length was: #{data.to_json.length}"
			log "ERROR: Send data packet was: #{data.to_json}"
		end
	end
	$stats[:gcms_successful] += result['success'].to_i
	$stats[:gcms_failed] += result['failure'].to_i
end

def stream(hash)
	config = $settings[hash]
	screen_name = config[:screen_name]
	log screen_name, "Adding new thread."
	opts = {
		:path=>"/1.1/user.json",
		:host=>"userstream.twitter.com",
		:oauth=>{
			:consumer_key=>CONSUMER_TOKEN,
			:consumer_secret=>CONSUMER_SECRET,
			:token=>config[:token],
			:token_secret=>config[:secret]
		}
	}
	last_reconnect = Time.now
	
	machine = EM.run do
		client = EM::Twitter::Client.connect(opts)
		$settings[hash][:client] = client
		unauthorized_count = 0
		
		client.each do |result|
			$stats[:bytes] += result.length
			data = JSON.parse(result)
			if data.has_key?("direct_message")
				data = data["direct_message"]
			end
			
			if data.has_key?("text") && data.has_key?("recipient") && data["recipient_id"]==config[:user_id]
				log screen_name, "DM."
				send_gcm(config, data, "dm")
				$stats[:dms] += 1
			elsif data.has_key?("text")
				if data["entities"]["user_mentions"].any?{|mention| mention["id"]==config[:user_id]}
					log screen_name, "Mention. #{data["text"]}"
					send_gcm(config, data, "mention")
					$stats[:mentions] += 1
				elsif data.has_key?("rewteeted_status") && data["retweeted_status"]["user"]["id"]==config[:user_id]
					log hash, "Retweet"
					send_gcm(config, data, "retweet")
					$stats[:mentions] += 1
				else
					$stats[:normal_tweets] += 1
				end
			elsif data.has_key?("event") && data["event"]=="favorite" && data["source"]["id"]!=config[:user_id]
				log screen_name, "Favorited"
				send_gcm(config, data, "favorite")
				$stats[:favorites] += 1
			end
		end
		
		client.on_forbidden { log screen_name, "Forbidden. o_O" }
		client.on_unauthorized do
			unauthorized_count += 1
			log screen_name, "Unauthorized. o_O ##{unauthorized_count}"
			if unauthorized_count > 15
				log screen_name, "Too many unauthorized messages. Stopping and deleting account."
				client.connection.stop
				$settings.delete hash
			end
		end
		client.on_reconnect { log screen_name, "Reconnect."; last_reconnect = Time.now ; $stats[:reconnects] += 1 }
		client.on_close do
			if (Time.now - last_reconnect) > 1
				log screen_name, "Closed."
				config[:client] = nil
			end
		end
		client.on_not_found { log screen_name, "Not found. o_O" }
		client.on_not_acceptable { log screen_name, "Not acceptable. o_O" }
		client.on_too_long { log screen_name, "Too long. o_O" }
		client.on_range_unacceptable { log screen_name, "Range unacceptable. o_O" }
		client.on_rate_limited { log screen_name, "Rate limited. o_O" }
	end
	$stats[:accounts] += 1
	$stats[:reg_ids] += config[:reg_ids].count
end

def log(screen_name, string=nil)
	screen_name, string = "", screen_name unless string
	puts "%s   %-20s   %s" % [Time.now, screen_name, string]
end

def print_stats
	str = $stats.collect{|key, value| "#{key}:#{value}"}.join(" ")
	File.open(File.join(File.dirname(__FILE__), "stats.txt"), "w") {|f| f.write(str)}
end

Thread.new do
	EM.run do
	end
end

EventMachine::PeriodicTimer.new(60) do
	print_stats()
end

$settings.each do |key, hash|
	stream(key)
end

update()

Listen.to(File.dirname(__FILE__), :filter=>/^command\.txt$/) do |modified, added, removed|
	if removed.empty?
		update()
	end
end
