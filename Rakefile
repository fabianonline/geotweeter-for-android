require 'rubygems'
require 'rake'

$target_dir = File.dirname(__FILE__) + "/build_temp"
$default_package = "de.geotweeter"
$target_package = $default_package
$app_name = "Geotweeter"
$replacements = []
$key_to_use = :debug

task :default=>:beta

desc "Build de.geotweeter with release keys."
task :full do
	$target_package = "de.geotweeter"
	$key_to_use = :release
#	$replacements << ["Log.d(", "// Log.d("]
	$replacements << ["public static final boolean RELEASE = false;", "public static final boolean RELEASE = true;"]
    $replacements << ['Utils.getProperty("google.maps.key.development")', 'Utils.getProperty("google.maps.key.release")']
	Rake::Task['copy_and_modify_files'].invoke
end

desc "Build de.geotweeter.beta with debug keys and logging."
task :beta do
	$target_package = "de.geotweeter.beta"
	$app_name = "Geotweeter Beta"
    $key_to_use = :release
	$replacements << [$default_package, $target_package]
    $replacements << ['Utils.getProperty("google.maps.key.development")', 'Utils.getProperty("google.maps.key.release")']
	Rake::Task['copy_and_modify_files'].invoke
end

desc "Build de.geotweeter.lite with release keys."
task :lite do
	$target_package = "de.geotweeter.lite"
	$app_name = "Geotweeter Lite"
	$key_to_use = :release
	$replacements << [$default_package, $target_package]
#	$replacements << ["Log.d(", "// Log.d("]
	$replacements << ["public static final boolean RELEASE = false;", "public static final boolean RELEASE = true;"]
    $replacements << ['Utils.getProperty("google.maps.key.development")', 'Utils.getProperty("google.maps.key.release")']
	Rake::Task['copy_and_modify_files'].invoke
end

task :tag_version do
    last_tag = %x(git describe --abbrev=0 --tags).strip
    last_tag = "(leer)" if last_tag == ""
    puts "Letztes Tag war '#{last_tag}'. Soll eine neue Version gesetzt und getaggt werden? (Leerlassen für 'Nein' oder Versionsbezeichnung eingeben.)"
    new_version = STDIN.gets.chomp
    if new_version != ""
        %x(git tag #{new_version})
    else
        new_version = last_tag
    end

    string = File.read($target_dir + "/AndroidManifest.xml")
    string = string.gsub(/android:versionName="(.+)"/, "android:versionName=\"#{new_version}\"")
    File.open($target_dir + "/AndroidManifest.xml", "w") {|f| f.write(string)}
end

task :copy_and_modify_files=>[:check_properties] do
	FileUtils.rm_r($target_dir, :force=>true)
	puts "Copying files..."
	list = FileList.new(File.dirname(__FILE__) + '/*').exclude("build_temp").to_a
	FileUtils.mkdir_p($target_dir)
	FileUtils.cp_r(list, $target_dir)
	
	if $target_package != $default_package
		puts "Changing namespace to #{$target_package}..."
		list = FileList.new($target_dir + "/src/" + $default_package.gsub('.', '/') + "/*").to_a
		FileUtils.mkdir_p($target_dir + "/src/" + $target_package.gsub('.', '/'))
		FileUtils.mv(list, $target_dir + "/src/" + $target_package.gsub('.', '/'))
	end
	
	puts "Setting app name..."
	string = File.read($target_dir + "/res/values/strings.xml")
	string = string.gsub(/<string name="app_name">.+<\/string>/, '<string name="app_name">' + $app_name + '</string>')
	File.open($target_dir + "/res/values/strings.xml", "w") {|f| f.write(string)}
	
	unless $replacements.empty?
		puts "Performing string replacements..."
		list = FileList.new($target_dir + "/**/*").exclude(/\.keystore$/)
		list.each do |file|
			next if File.directory?(file)
			text = File.read(file)
			$replacements.each {|rep| text = text.gsub(rep[0], rep[1])}
			File.open(file, "w") {|f| f.write(text)}
		end
	end
	
	puts "Cleaning folders..."
	FileUtils.rm_rf($target_dir + "/gen")
	FileUtils.mkdir($target_dir + "/gen")
	FileUtils.rm_rf($target_dir + "/bin")
	FileUtils.mkdir($target_dir + "/bin")
	
	puts "Setting Version information..."
	Rake::Task['tag_version'].invoke
	commit = %x(git describe --tags --always --dirty)
	string = File.read($target_dir + "/AndroidManifest.xml")
	string = string.gsub(/android:versionName="(.+)"/, "android:versionName=\"#{commit.strip}\"")
	commit_count = %x(git rev-list --all | wc -l)
	string = string.gsub(/android:versionCode="(.+)"/, "android:versionCode=\"#{commit_count.strip}\"")
	File.open($target_dir + "/AndroidManifest.xml", "w") {|f| f.write(string)}
	
	puts "Adding keystore information..."
	string = File.read($target_dir + "/ant.properties")
	string << "\n\n"
	if $key_to_use == :release
		string << "key.store=release.keystore\n"
		string << "key.alias=de.geotweeter\n"
	else
		string << "key.store=debug.keystore\n"
		string << "key.alias=androiddebugkey\n"
		string << "key.store.password=android\n"
		string << "key.alias.password=android\n"
	end
	File.open($target_dir + "/ant.properties", "w") {|f| f.write(string)}
	
	Rake::Task['ant'].invoke
end

task :check_properties do
    properties_to_check = %w(twitter.consumer.key twitter.consumer.secret tweetmarker.key google.gcm.sender.id google.gcm.sender.token google.gcm.server.url twitpic.key google.maps.key.development google.maps.key.release crashreport.server.url cacert.keystore.passphrase)

    lines = File.readlines("./res/raw/geotweeter.properties")
    hash = Hash[ lines.collect {|l| l.strip.split("=", 2)} ]
    properties_to_check.each {|p| fail "Property '#{p}' is missing or empty in res/raw/geotweeter.properties." unless hash.has_key?(p) && hash[p]}
end

task :ant do
	puts "Calling ant."
	sh "ant", "release", "-buildfile", $target_dir+"/build.xml"
	Rake::Task['clean'].invoke
end

task :clean do
	puts "Moving apk to bin/..."
	FileUtils.mv("#{$target_dir}/bin/#{$target_package}-release.apk", "bin/#{$target_package}.apk")
	
	puts "Cleaning up..."
	FileUtils.rm_rf($target_dir)
	
	puts
	puts "DONE."
	puts "File is in bin/#{$target_package}.apk"
    
    puts
    puts "Please remember to push all newly created tags to the central repository by using 'git push --tags'."
end


