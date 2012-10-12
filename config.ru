require 'rubygems'
require 'rack'
require 'sinatra'

require './crashes'
run Sinatra::Application
