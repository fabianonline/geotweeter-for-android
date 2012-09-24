#!/usr/bin/ruby

require 'rubygems'
require 'bundler'
require 'digest/sha2'
Bundler.require

Thread.abort_on_exception = true
$settings = YAML::load_file("settings.yml") rescue {}
$digest = Digest::SHA256.new

$gcm_sender = HiGCM::Sender.new("AIzaSyCkOw9l2iZhjytnKNmL8EiWmZZtcco6lik")

def update()
	lines = IO.readlines("command.txt")
	lines.each do |line|
		command, token, secret, reg_id = *line.split(" ")
		hash_value = $digest.hexdigest([token, secret].join).slice(0, 7)
		if command=="add"
			next unless reg_id && reg_id.length>5
			if $settings.has_key? hash_value
				next if $settings[hash_value].has_key?(:reg_ids) && $settings[hash_value][:reg_ids].include?(reg_id)
				$settings[hash_value][:reg_ids] << reg_id
				$settings[hash_value][:thread].kill if $settings[hash_value][:thread]
				log("Adding new reg_id to #{hash_value}")
			else
				hash = {:token=>token, :secret=>secret, :reg_ids=>[reg_id]}
				$settings[hash_value] = hash
				log("Adding stream for #{hash_value}")
			end
			start_thread(hash_value)
		elsif command=="del"
			puts "Del. #{hash_value}"
			next unless $settings.has_key? hash_value
			$settings[hash_value][:thread].kill if $settings[hash_value][:thread]
			$settings[hash_value][:reg_ids].delete reg_id
			if $settings[hash_value][:reg_ids].empty?
				$settings.delete hash_value
				log("Stream for #{hash_value} has no more reg_ids left.")
			else
				log("Removed reg_id from #{hash_value}")
				start_thread(hash_value)
			end
		end
	end
	save_settings
end

def save_settings
	clone = $settings.clone
	copy = {}
	clone.each do |key, hash|
		hash.delete :thread
		copy[key] = hash
	end
	File.open(File.join(File.dirname(__FILE__), "settings.yml"), "w") {|f| f.write YAML.dump(copy)}
end

def stream(hash)
	data = $settings[hash]
	log hash, "New thread is running. Settings: " + data.inspect
	#result = $gcm_sender.send(data[:reg_ids], {:data=>{:msg=>"Hallo Welt"}})
	while true
		log hash, "ich lebe noch."
		sleep (10)
	end
	p result
end

def start_thread(hash)
	t = Thread.new do 
		stream(hash)
	end
	$settings[hash][:thread] = t
end

def log(hash, string=nil)
	hash, string = "", hash unless string
	puts "%-10s %s" % [hash, string]
end

$settings.each do |key, hash|
	start_thread(key)
end


Listen.to(File.dirname(__FILE__), :filter=>/^command\.txt$/) do |modified, added, removed|
	log "New command found."
	if removed.empty?
		update()
		#FileUtils.rm(File.join(File.dirname(__FILE__), "command.txt"))
	end
end

