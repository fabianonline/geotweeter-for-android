#!/usr/bin/ruby
require 'lib/core.rb'
Bundler.require(:service)
require 'base64'
require 'zlib'

# Read the properties file and save it's contents in $properties
$properties = {}
IO.readlines(File.join(File.dirname(__FILE__), "geotweeter.properties")).each do |l|
	parts=l.split("=", 2)
	next unless parts[1]
	$properties[parts[0]] = parts[1].strip
end

Thread.abort_on_exception = true
$gcm_sender = HiGCM::Sender.new($properties['google.gcm.sender.token'])
$CONSUMER_TOKEN  = $properties['twitter.consumer.key']
$CONSUMER_SECRET = $properties['twitter.consumer.secret']


def read_updated_commands_file
	(lines = IO.readlines("command.txt")) rescue return
	lines.each do |line|
		command, token, secret, reg_id, screen_name, version = *line.split(" ")
		(twitter_id = token.split("-")[0].to_i) rescue next
		if command=="add"
			next unless reg_id && reg_id.length>5
			# retrieve the user object from the DB
			user = User.instances[twitter_id] || User.first_or_new(:twitter_id=>twitter_id)
			new_user = user.new?
			user.attributes = {:twitter_token=>token, :twitter_secret=>secret, :twitter_screen_name=>screen_name}
			user.save
			reg_id = user.reg_ids.first_or_new(:reg_id=>reg_id)
			new_reg_id = reg_id.new?
			reg_id.version = version
			reg_id.save
			if new_user
				user.start_stream()
			else
				if new_reg_id
					user.log("New device.")
				else
					user.log("Re-Registration.")
					Stats.add_re_registration()
				end
			end
		end
	end
	FileUtils.rm(File.join(File.dirname(__FILE__), "command.txt"))
end

Thread.new do
	EM.run do
	end
end

puts "Adding Timer for Stats..."
EventMachine::PeriodicTimer.new(60) do
	Stats.save()
end

User.all.each do |user|
	user.start_stream()
end

read_updated_commands_file()

Listen.to(File.dirname(__FILE__), :filter=>/^command\.txt$/) do |modified, added, removed|
	if removed.empty?
		read_updated_commands_file()
	end
end
