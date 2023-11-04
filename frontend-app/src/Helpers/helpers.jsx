export function createRoundedRange(value) {
  // Calculate the start of the rounded range
  const start = Math.ceil(value / 10) * 10 // Round up to the nearest 10th

  // Calculate the end of the range (e.g., 10 - 20)
  const end = Math.ceil(start + start * 0.3)

  // Construct the range string
  const rangeString = `${start} - ${end}`

  return rangeString
}
