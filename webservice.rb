$groups_to_require = [:webservice]
require 'lib/core.rb'

post '/register' do
	File.open(File.join(File.dirname(__FILE__), "command.txt"), "a") do |f|
		f.write "\n"+(["add", params[:token], params[:secret], params[:reg_id], params[:screen_name], (params[:version] || 0)].join(" "))+"\n"
	end
	puts "ok"
end

get '/' do
	return "It works!"
end