#!/usr/bin/ruby

require 'rubygems'
require 'bundler'
require 'digest/sha2'
Bundler.require

Thread.abort_on_exception = true
$settings = YAML::load_file("settings.yml") rescue {}
$digest = Digest::SHA256.new

$gcm_sender = HiGCM::Sender.new("AIzaSyCkOw9l2iZhjytnKNmL8EiWmZZtcco6lik")
CONSUMER_TOKEN  = "7tbUmgasX8QXazkxGMNw"
CONSUMER_SECRET = "F22QSxchkczthiUQomREXEu4zDA15mxiENNttkkA"

def update()
	lines = IO.readlines("command.txt")
	lines.each do |line|
		command, token, secret, reg_id = *line.split(" ")
		hash_value = $digest.hexdigest([token, secret].join).slice(0, 7)
		if command=="add"
			next unless reg_id && reg_id.length>5
			if $settings.has_key? hash_value
				if $settings[hash_value].has_key?(:reg_ids) && $settings[hash_value][:reg_ids].include?(reg_id)
					log "Not adding client: Already known."
					next
				end
				$settings[hash_value][:reg_ids] << reg_id
				$settings[hash_value][:client].connection.stop if $settings[hash_value][:client]
				log("Adding new reg_id to #{hash_value}")
			else
				hash = {:token=>token, :secret=>secret, :reg_ids=>[reg_id]}
				hash[:user_id] = /^([0-9]+)-/.match(token)[1].to_i
				$settings[hash_value] = hash
				log("Adding stream for #{hash_value}")
			end
			stream(hash_value)
		elsif command=="del"
			next unless $settings.has_key? hash_value
			$settings[hash_value][:client].connection.stop if $settings[hash_value][:client]
			$settings[hash_value][:reg_ids].delete reg_id
			if $settings[hash_value][:reg_ids].empty?
				$settings.delete hash_value
				log("Stream for #{hash_value} has no more reg_ids left.")
			else
				log("Removed reg_id from #{hash_value}")
				stream(hash_value)
			end
		end
	end
	save_settings
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
	data = JSON.parse(result.body)
	log data.inspect unless data["success"]==1
end

def stream(hash)
	config = $settings[hash]
	log hash, "Adding new thread. Settings: " + config.inspect
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
	
	machine = EM.run do
		client = EM::Twitter::Client.connect(opts)
		$settings[hash][:client] = client
		
		client.each do |result|
			data = JSON.parse(result)
			if data.has_key?("direct_message")
				data = data["direct_message"]
			end
			
			if data.has_key?("text") && data.has_key?("recipient") && data["recipient_id"]==config[:user_id]
				log hash, "DM."
				send_gcm(config, data, "dm")
			elsif data.has_key?("text")
				if data["entities"]["user_mentions"].any?{|mention| mention["id"]==config[:user_id]}
					log hash, "Mention. #{data["text"]}"
					send_gcm(config, data, "mention")
				elsif data.has_key?("rewteeted_status") && data["retweeted_status"]["user"]["id"]==config[:user_id]
					log hash, "Retweet"
					send_gcm(config, data, "retweet")
				end
			elsif data.has_key?("event") && data["event"]=="favorite" && data["source"]["id"]!=config[:user_id]
				log hash, "Favorited"
				send_gcm(config, data, "favorite")
			end
		end
		
		client.on_forbidden do
			log hash, "Forbidden. o_O"
		end
		
		client.on_reconnect do
			log hash, "Reconnected."
		end
		
		client.on_close do
			log hash, "Stopped."
		end
	end
end

def log(hash, string=nil)
	hash, string = "", hash unless string
	puts "%s   %-10s %s" % [Time.now, hash, string]
end

Thread.new do
	EM.run do
	end
end

$settings.each do |key, hash|
	stream(key)
end

Listen.to(File.dirname(__FILE__), :filter=>/^command\.txt$/) do |modified, added, removed|
	log "New command(s) found."
	if removed.empty?
		update()
		#FileUtils.rm(File.join(File.dirname(__FILE__), "command.txt"))
	end
end

