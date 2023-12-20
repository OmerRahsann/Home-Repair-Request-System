import React, { useState } from 'react'
import { Rating } from '@smastrom/react-rating'

const Review = ({ review }) => {
  //const { rating, customerName, comment } = review;
  const [isExpanded, setIsExpanded] = useState(false)

  const toggleExpand = () => {
    setIsExpanded(!isExpanded)
  }

  return (
    <div className="bg-white border border-gray-300 rounded p-4 overflow-hidden shadow-md m-4">
      <div className="text-lg font-semibold mb-2">Customer Name</div>
      <Rating value={4} style={{ maxWidth: 75 }} />

      <div
        className={`text-sm overflow-hidden ${
          isExpanded
            ? 'whitespace-normal'
            : 'overflow-ellipsis whitespace-nowrap'
        }`}
      >
        Commentdfshjasdfhjadshfjads asdjhfjahdsf hjdsh sad sdfh jhdsfjasd
        hjasdfhjadsfdasjhjhasd fhjasdf asdfhj hsafd
      </div>

      <button
        className="text-blue-500 mt-2 cursor-pointer text-xs"
        onClick={toggleExpand}
      >
        {isExpanded ? 'Show Less' : 'Show More'}
      </button>
    </div>
  )
}

export default Review
