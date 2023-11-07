import React from 'react'

const CircleMarker = ({ lat, lng, radius }) => {
  const circleStyle = {
    width: `${radius * 2}px`,
    height: `${radius * 2}px`,
    borderRadius: '50%',
    backgroundColor: 'blue', // You can customize the color
    opacity: 0.4, // You can adjust the opacity
    position: 'absolute',
    top: `calc(50% - ${radius}px)`,
    left: `calc(50% - ${radius}px)`,
  }

  return (
    <div style={{ position: 'absolute', top: lat, left: lng }}>
      <div style={circleStyle}></div>
    </div>
  )
}

export default CircleMarker
