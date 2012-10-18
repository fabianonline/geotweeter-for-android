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
	crash = Crash.new
	Crash.properties.each do |p|
		crash.send("#{p.name}=".to_sym, params[p.name.to_s.upcase.to_sym]) rescue nil
	end
	crash.save

	room = Broach::Room.find_by_name(ROOM)
	
	string = "Neuer Crashreport. "
	string << "Kommentar des Users: #{crash.user_comment} " if crash.user_comment
	string << url("/crash/#{crash.id}")
	room.speak(string, :type=>:text)
	room.speak(crash.short_stacktrace, :type=>:paste)
end

get '/crash/:id' do
	@crash = Crash.get(params[:id])
	erb :show_long
end

post '/crash/:id/fixed' do
	Crash.get(params[:id]).update(:fixed=>true)
	redirect to('/')
end

get '/' do
	@crashes = Crash.all(:order => [:id.desc])
	erb :index
end
