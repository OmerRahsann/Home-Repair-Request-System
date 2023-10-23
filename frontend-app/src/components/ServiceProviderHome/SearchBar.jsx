import React, { useState } from 'react'

function SearchBar({ onSearch }) {
  const [inputValue, setInputValue] = useState('')

  const handleSearch = () => {
    onSearch(inputValue)
  }

  return (
    <div>
      <input
        type="text"
        value={inputValue}
        onChange={(e) => setInputValue(e.target.value)}
        placeholder="Search by keyword..."
      />
      <button onClick={handleSearch}>Search</button>
    </div>
  )
}

export default SearchBar
