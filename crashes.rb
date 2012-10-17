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

post '/send' do
	crash = Crash.new
	Crash.properties.each do |p|
		crash.send("#{p.name}=".to_sym, params[p.name.to_s.upcase.to_sym]) rescue nil
		#halt "set #{p.name} to #{p.name.to_s.upcase.to_sym}: #{params[p.name.to_s.upcase.to_sym]}. #{params.inspect}"
	end
	crash.save
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
