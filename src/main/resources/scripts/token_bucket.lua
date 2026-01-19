-- Token Bucket algorithm for Redis
--
-- Runs atomically - no race conditions between instances
--
-- Call: EVALSHA <sha> 1 <key> <capacity> <refill_per_second> <now_millis>
-- Returns: [allowed (0/1), remaining, retry_after_millis]

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_per_second = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Load current state (or initialize with full bucket)
local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(bucket[1]) or capacity
local last_refill = tonumber(bucket[2]) or now

-- Calculate refill based on elapsed time
local elapsed = (now - last_refill) / 1000.0
local refilled = math.min(capacity, tokens + elapsed * refill_per_second)

if refilled >= 1 then
    -- Allowed: consume 1 token
    local new_tokens = refilled - 1
    redis.call('HMSET', key, 'tokens', new_tokens, 'last_refill', now)
    redis.call('EXPIRE', key, 3600)
    return {1, math.floor(new_tokens), 0}
else
    -- Rejected: calculate retry time
    local needed = 1 - refilled
    local retry_ms = math.ceil((needed / refill_per_second) * 1000)
    redis.call('HMSET', key, 'tokens', refilled, 'last_refill', now)
    redis.call('EXPIRE', key, 3600)
    return {0, 0, retry_ms}
end
