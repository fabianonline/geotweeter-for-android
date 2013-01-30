require 'rubygems'
require 'bundler'
Bundler.require(:default, *$groups_to_require || "")

DataMapper::Logger.new($stdout, :debug)
DataMapper.setup(:default, 'sqlite:database.db')
DataMapper::Model.raise_on_save_failure = true

require 'lib/reg_id.rb'
require 'lib/user.rb'
require 'lib/stats.rb'

DataMapper.finalize

DataMapper.auto_upgrade!