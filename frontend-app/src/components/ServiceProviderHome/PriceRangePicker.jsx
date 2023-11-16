import React, { useState } from 'react'

const PriceRangePicker = () => {
  const [selectedRange, setSelectedRange] = useState(null)
  const [customRange, setCustomRange] = useState('')

  const handleRangeChange = (event) => {
    setSelectedRange(event.target.value)
  }

  const handleCustomRangeChange = (event) => {
    setCustomRange(event.target.value)
    // Clear selectedRange when custom range is being input
    setSelectedRange(null)
  }

  return (
    <div className="flex flex-col">
      <label className="text-lg font-semibold mb-2">
        Select or Enter Price Range:
      </label>

      <div className="flex items-center mb-4">
        <input
          type="radio"
          id="range1"
          value="$"
          checked={selectedRange === '$'}
          onChange={handleRangeChange}
          className="mr-2"
        />
        <label htmlFor="range1" className="mr-4">
          $
        </label>

        <input
          type="radio"
          id="range2"
          value="$$"
          checked={selectedRange === '$$'}
          onChange={handleRangeChange}
          className="mr-2"
        />
        <label htmlFor="range2" className="mr-4">
          $$
        </label>

        <input
          type="radio"
          id="range3"
          value="$$$"
          checked={selectedRange === '$$$'}
          onChange={handleRangeChange}
          className="mr-2"
        />
        <label htmlFor="range3">$$$</label>
      </div>

      <div className="mb-4">
        <label className="text-sm">Or enter custom range:</label>
        <input
          type="text"
          value={customRange}
          onChange={handleCustomRangeChange}
          className="border p-2 rounded-md w-32"
        />
      </div>

      <p>
        Selected Range: {selectedRange || 'None'}
        {customRange && <span>, Custom Range: {customRange}</span>}
      </p>
    </div>
  )
}

export default PriceRangePicker
