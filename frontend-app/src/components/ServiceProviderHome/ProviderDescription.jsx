import React, { useState } from 'react'

function ProviderDescription(props) {
  const [description, setDescription] = useState('')
  const maxCharacters = 100

  const handleDescriptionChange = (event) => {
    const text = event.target.value

    if (text.length <= maxCharacters) {
      setDescription(text)
      props.onDescriptionChange(text)
    }
  }

  return (
    <div className="bg-custom-gray items-center">
      <textarea
        value={description}
        onChange={handleDescriptionChange}
        placeholder="Provider Description: Enter a description of your services..."
        className="w-full p-2 text-xs outline-none bg-custom-gray"
        maxLength={maxCharacters}
        spellCheck
        style={{ resize: 'none' }}
        required
      />
      <p className="text-xs text-gray-400 text-center">
        Characters remaining: {maxCharacters - description.length}
      </p>
    </div>
  )
}

export default ProviderDescription
