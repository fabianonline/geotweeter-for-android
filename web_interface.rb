#!/usr/bin/ruby

require 'rubygems'
require 'yaml'


post '/register' do
	File.open(File.join(File.dirname(__FILE__), "command.txt"), "a") do |f|
		f.write ["add", params[:token], params[:secret], params[:reg_id]].join(" ")
	end

	puts "ok"
end

get '/' do
	"it works"
end
