/**
 * Tells how much to sell day-by-day with respect to (current market) value by computing
 *
 *        value
 * ------------------- * remaining possession
 * value + future_value
 *
 * where future_value = selling_days_remaining * future_value_prediction.
 * When selling_days_remaining = 0, anything remaining is sold. Should behave smoothly though.
 *
 * Actually, the real idea is this:
 *
 *      v * a^q
 * -------------------- * remaining possession
 * v * a^q + (r^p * f)
 *
 * where p and q are risk parameters:
 * A higher q makes this seller more sensitive to deviations from prediction.
 * A higher p makes it more confident to wait on selling due to a positive outlook.
 * a is the ratio between current value and predicted current value.
 * Safest seller does q = 0 and p = 1.
 * Only the a^q is implemented for having somewhat use.
 */

Full code isn't here though. Moved it elsewhere.
