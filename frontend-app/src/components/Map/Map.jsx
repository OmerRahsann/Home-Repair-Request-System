import React, { useState, useEffect } from 'react'
import GoogleMapReact from 'google-map-react'
import { Marker } from '@react-google-maps/api'
import { FaMapMarkerAlt } from 'react-icons/fa'
import CircleMarker from './CircleMarker'

const Map = ({ places, address, isProvider }) => {
  //const matches = useMediaQuery('(min-width:600px)');
  //const classes = useStyles();
  const [latLng, setLatLng] = useState(null)

  const reverseGeocode = async () => {
    try {
      const response = await fetch(
        `https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=${process.env.REACT_APP_GOOGLE_API_KEY}`,
      )

      if (response.ok) {
        const data = await response.json()
        if (data.results.length > 0) {
          const location = data.results[0].geometry.location
          setLatLng(location)
          console.log('Latitude:', location.lat)
          console.log('Longitude:', location.lng)
          return latLng
        } else {
          console.error('No results found for the given address.')
        }
      } else {
        console.error('Geocoding request failed:', response.statusText)
      }
    } catch (error) {
      console.error('Error during geocoding request:', error)
    }
  }

  useEffect(() => {
    reverseGeocode().then((location) => {
      if (location) {
        setLatLng(location)
      }
    })
  }, [address])

  return (
    <div className="h-[85vh] w-100%">
      {latLng && (
        <GoogleMapReact
          bootstrapURLKeys={{ key: process.env.REACT_APP_GOOGLE_API_KEY }}
          defaultCenter={latLng}
          center={latLng}
          defaultZoom={18}
          zoom={17}
          margin={[50, 50, 50, 50]}
          draggable={false}
          options={{ gestureHandling: 'none', disableDefaultUI: true }}
        >
          {!isProvider ? (
            <Marker lat={latLng.lat} lng={latLng.lng}>
              {/* You can customize the marker content here */}
              <FaMapMarkerAlt
                color="red"
                title={address}
                style={{
                  fontSize: '3em',
                  position: 'relative',
                  top: '-1em',
                  left: '-0.5em',
                }}
              />
            </Marker>
          ) : (
            <CircleMarker lat={latLng.lat} lng={latLng.lng} radius={250} />
          )}
        </GoogleMapReact>
      )}
    </div>
  )
}

export default Map
