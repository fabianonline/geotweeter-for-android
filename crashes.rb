#!/usr/bin/ruby

require 'rubygems'
require 'bundler'
Bundler.require(:default)
require 'dm-migrations'
require 'dm-validations'
require 'erb'
require 'models.rb'


DataMapper.setup(:default, "sqlite:database.db")
DataMapper.finalize
DataMapper.auto_upgrade!

$properties = {}
IO.readlines(File.join(File.dirname(__FILE__), "geotweeter.properties")).each do |l|
	parts=l.split("=", 2)
	next unless parts[1]
	$properties[parts[0]] = parts[1].strip
end

Broach.settings = {'account'=>$properties['campfire.account'], 'token'=>$properties['campfire.token'], 'use_ssl'=>true}

if defined?(::Sinatra) && defined?(::Sinatra::Base)
	#log = File.new("log.log", "a+")
	#$stdout.reopen(log)
	#$stderr.reopen(log)

	post '/send' do
		halt(200) if Crash.first(:report_id=>params[:REPORT_ID]) != nil
		crash = Crash.new
		Crash.properties.each do |p|
			crash.send("#{p.name}=".to_sym, params[p.name.to_s.upcase.to_sym]) rescue nil
		end
		crash.calculate_hash
		if crash.save
			room = Broach::Room.find_by_name($properties['campfire.room'])
			
			string = "Neuer Crashreport ##{crash.id}. "
			
			if (count=Crash.count(:crash_hash=>crash.crash_hash)) > 1
				string << "Bekannter Typ (#{url("/crashes/#{crash.crash_hash}")}). Jetzt #{count} Vorkommnisse. "
			else
				string << "Neuer Typ. Noch keine Vorkommnisse. "
			end
			string << "Kommentar des Users: #{crash.user_comment} " if crash.user_comment
			string << "User: #{crash.email} " unless crash.email==""
			string << url("/crash/#{crash.id}")
			room.speak(string, :type=>:text)
			room.speak(crash.short_stacktrace, :type=>:paste)
		else
			error 500, "Crash konnte nicht gespeichert werden. Fehler: \n#{crash.errors.collect(&:to_s).join("\n")}"
		end
	end

	get '/crash/:id' do
		@crash = Crash.get(params[:id])
		erb :show_long
	end

	post '/crash/:id/fixed' do
		Crash.get(params[:id]).update(:fixed=>true)
		redirect to('/')
	end

	post '/crash/:id/notfixed' do
		Crash.get(params[:id]).update(:fixed=>false)
		redirect to('/')
	end
	
	get '/crashes/:hash' do
		@crashes = Crash.all(:crash_hash=>params[:hash], :order=>[:id.desc])
		erb :crash_list
	end
	
	post '/crashes/:hash/fixed' do
		Crash.all(:crash_hash=>params[:hash]).update(:fixed=>true)
		redirect to('/')
	end
	
	get '/' do
		@crashes = []
		last_hash = nil
		Crash.all(:fixed=>false, :order=>:crash_hash).each do |crash|
			if last_hash && last_hash[:hash] == crash.crash_hash
				last_hash[:count] += 1
				last_hash[:date_last] = crash.user_crash_date if last_hash[:date_last]==nil || last_hash[:date_last]<crash.user_crash_date
				last_hash[:date_first] = crash.user_crash_date if last_hash[:date_first]>crash.user_crash_date
			else
				last_hash = {:count=>1, :hash=>crash.crash_hash, :stack_trace=>crash.stack_trace, :fixed=>false, :date_first=>crash.user_crash_date, :date_last=>nil}
				@crashes << last_hash
			end
		end
		@crashes = @crashes.sort_by{|elm| elm[:count]}.reverse
		erb :new_layout
	end
end
