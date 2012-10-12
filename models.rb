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
	property :build, String
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
end
