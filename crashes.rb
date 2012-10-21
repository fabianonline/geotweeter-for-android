#!/usr/bin/ruby

require 'rubygems'
require 'bundler'
Bundler.require(:default)
require 'dm-migrations'
require 'erb'
require 'models.rb'

DataMapper.setup(:default, "sqlite:database.db")
DataMapper.finalize
DataMapper.auto_upgrade!

Broach.settings = {'account'=>'geotweeter', 'token'=>'1fcf39a7b2cbf17d77d7fabf81a5d6ec27f17f18', 'use_ssl'=>true}
ROOM = "Geotweeter"

post '/send' do
	halt(200) if Crash.first(:report_id=>params[:REPORT_ID]) != nil
	crash = Crash.new
	Crash.properties.each do |p|
		crash.send("#{p.name}=".to_sym, params[p.name.to_s.upcase.to_sym]) rescue nil
	end
	if crash.save

		room = Broach::Room.find_by_name(ROOM)
		
		string = "Neuer Crashreport ##{crash.id}. "
		string << "Kommentar des Users: #{crash.user_comment} " if crash.user_comment
		string << "User: #{crash.email} " unless crash.email==""
		string << url("/crash/#{crash.id}")
		room.speak(string, :type=>:text)
		room.speak(crash.short_stacktrace, :type=>:paste)
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

get '/' do
	@crashes = Crash.all(:order => [:id.desc])
	erb :index
end
