class Stats
	@normal_tweets=0
	@mentions=0
	@dms=0
	@bytes=0
	@accounts=0
	@reg_ids=0
	@gcms_successful=0
	@gcms_failed=0
	@favorites=0
	@retweets=0
	@re_registrations=0
	@reconnects=0

	def self.add_normal_tweet; @normal_tweets+=1; end
	def self.add_mention; @mentions+=1; end
	def self.add_dm; @dms+=1; end
	def self.add_bytes(number); @bytes+=number; end
	def self.add_account; @accounts+=1; end
	def self.add_reg_id(number=1); @reg_ids+=number; end
	def self.add_successful_gcms(number); @gcms_successful+=number; end
	def self.add_failed_gcms(number); @gcms_failed+=number; end
	def self.add_favorite; @favorites+=1; end
	def self.add_retweet; @retweets+=1; end
	def self.add_re_registration; @re_registrations+=1; end
	def self.add_reconnect; @reconnects+=1; end

	def self.save
		str = self.instance_variables.collect{|var| "#{var[1..-1]}:#{Stats.instance_variable_get(var)}"}.join(" ")
		File.open("stats.txt", "w") {|f| f.write(str)}
	end
end