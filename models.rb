class Crash
	include DataMapper::Resource

	property :id, Serial
	property :report_id, String
	property :app_version_code, Integer
	property :app_version_name, String
	property :package_name, String
	property :file_path, String
	property :phone_model, String
	property :android_version, String
	property :build, Text
	property :brand, String
	property :product, String
	property :total_mem_size, Integer
	property :available_mem_size, Integer
	property :custom_data, Text
	property :stack_trace, Text
	property :initial_configuration, Text
	property :crash_configuration, Text
	property :display, Text
	property :user_comment, Text, :lazy=>false
	property :user_app_start_date, DateTime
	property :user_crash_date, DateTime
	property :dumpsys_meminfo, Text
	property :dropbox, Text
	property :logcat, Text, :lazy=>false
	property :installation_id, String
	property :user_email, String
	property :device_features, Text
	property :environment, Text
	property :settings_system, Text
	property :settings_secure, Text
	property :shared_preferences, Text
    property :application_log, Text
    property :media_codec_list, Text
	property :thread_details, Text, :lazy=>false

	property :fixed, Boolean, :default=>false, :required=>true
	property :crash_hash, String

	def self.short_stacktrace(stack_trace)
		take_next_line = true
		stack_trace.split("\n").collect do |line|
			result = nil
			if line[0]!=9
				take_next_line=true
				result = line
			elsif take_next_line
				take_next_line=false
				result = line
			end
			result
		end.compact.join("\n")
	end

	def Crash.generalize_stacktrace(stack_trace)
		stack_trace.
			gsub(/=[0-9]+KB/, "=...KB").
		    gsub(/com\.alibaba\.fastjson\.JSONException: expect .+ at .+\n\t/m, "com.alibaba.fastjson.JSONException: expect [...]\n\t");
	end

	def short_stacktrace
		Crash.short_stacktrace(self.stack_trace)
	end

	def email
		(/acra\.user\.email=(.+)/.match(shared_preferences)[1] || "") rescue ""
	end

	def calculate_hash
		require 'digest/sha2'
		string = Crash.generalize_stacktrace(self.short_stacktrace)
		self.crash_hash = (Digest::SHA2.new << string).to_s.slice(0,7)
	end
end
