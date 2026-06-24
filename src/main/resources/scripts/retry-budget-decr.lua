local current = tonumber(redis.call('get', KEYS[1]))
if current and current >= tonumber(ARGV[1]) then
	return redis.call('decrby', KEYS[1], ARGV[1])
end
return -1