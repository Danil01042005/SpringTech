local current = tonumber(redis.call('get', KEYS[1]))
local refill_amount = tonumber(ARGV[1])
local max = tonumber(ARGV[2])

if current then
	local new_value = math.min(current + refill_amount, max)
	redis.call('set', KEYS[1], new_value)
	return new_value
end
return -1