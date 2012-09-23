#!/usr/bin/ruby

require 'rubygems'
require 'bundler'
require 'digest/sha2'
Bundler.require

$settings = YAML::load_file("settings.yml") rescue {}
$digest = Digest::SHA256.new

$gcm_sender = HiGCM::Sender.new("AIzaSyCkOw9l2iZhjytnKNmL8EiWmZZtcco6lik")

def update()
	lines = IO.readlines("command.txt")
	lines.each do |line|
		command, token, secret, reg_id = *line.split(" ")
		hash_value = $digest.hexdigest([token, secret, reg_id].join).slice(0, 7)
		if command=="add"
			next if $settings.has_key? hash_value
			next unless reg_id && reg_id.length>5
			hash = {:token=>token, :secret=>secret, :reg_id=>reg_id}
			clone = hash.clone
			t = Thread.new {
				stream(clone, hash_value)
			}
			hash[:thread] = t
			$settings[hash_value] = hash
		elsif command=="del"
			puts "Del. #{hash_value}"
			next unless $settings.has_key? hash_value
			puts "Killing Thread."
			$settings[hash_value][:thread].kill
			$settings.delete hash_value
			p $settings
		end
	end
	save_settings
end

def save_settings
	data = $settings.clone
	new_hash = {}
	data.each {|key, hash| hash.delete(:thread); new_hash[key] = hash}
	File.open(File.join(File.dirname(__FILE__), "settings.yml"), "w") {|f| f.write YAML.dump(new_hash)}
end

def stream(data, hash_value)
	puts "Ich bin ein neuer Stream!"
	result = $gcm_sender.send([data[:reg_id]], {:data=>{:msg=>"Hallo Welt"}})
	puts "completed."
	p result
end

$settings.each do |key, hash|
	clone = hash.clone
	t = Thread.new { stream(clone, key) }
	hash[:thread] = t
end

p $settings

Listen.to(File.dirname(__FILE__), :filter=>/^command\.txt$/) do |modified, added, removed|
	if removed.empty?
		update()
		FileUtils.rm(File.join(File.dirname(__FILE__), "command.txt"))
	end
end

