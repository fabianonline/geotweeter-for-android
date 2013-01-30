class RegId
	include DataMapper::Resource

	property :id, Serial
	property :reg_id, String, :required=>true, :length=>255
	property :version, Integer, :default=>0

	belongs_to :user
end