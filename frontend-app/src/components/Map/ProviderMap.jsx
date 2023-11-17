import React, { useState, useEffect } from 'react'
import mapStyles from '../../mapStyles'
import GoogleMap from 'google-maps-react-markers'
import useStyles from './styles.js'
import noImage from '../../Pictures/noImage.jpeg'
import { Paper, useMediaQuery, CircularProgress } from '@material-ui/core'
import { createRoundedRange } from '../../Helpers/helpers'

const ProviderMap = ({
  coords,
  requests,
  setCoords,
  setBounds,
  onCardClicked,
  selectedLocation,
}) => {
  const matches = useMediaQuery('(min-width:600px)')
  const classes = useStyles()
  const [mapReady, setMapReady] = useState(false)
  const [c, setC] = useState(coords)
  const [mapKey, setMapKey] = useState(0)
  console.log({ c })

  const handleMapChange = (map) => {
    console.log(map)
    const newBounds = {
      ne: {
        lat: map.bounds.eb.hi,
        lng: map.bounds.La.hi,
      },
      sw: {
        lat: map.bounds.eb.lo,
        lng: map.bounds.La.lo,
      },
    }

    setBounds(newBounds)
  }

  const handleMapLoad = (map) => {
    setMapReady(true)
  }

  useEffect(() => {
    if (mapReady) {
      // Fetch or update data and set it in the state
    }
  }, [mapReady])

  useEffect(() => {
    setC(coords)
    setMapKey((prevKey) => prevKey + 1) // Increment the key to trigger a re-render
  }, [coords])

  useEffect(() => {
    if (selectedLocation) {
      setCoords(selectedLocation)
    }
  }, [selectedLocation])

  return (
    <div className={classes.mapContainer}>
      {Object.keys(c).length !== 0 ? (
        <GoogleMap
          key={mapKey}
          apiKey={process.env.REACT_APP_GOOGLE_API_KEY}
          defaultCenter={coords}
          center={coords}
          defaultZoom={13}
          options={{
            disableDefaultUI: true,
            zoomControl: true,
            styles: mapStyles,
          }}
          margin={[50, 50, 50, 50]}
          onChange={handleMapChange}
          onLoad={handleMapLoad}
        >
          {requests.length &&
            requests.map((request, i) => (
              <div
                className={classes.markerContainer}
                lat={Number(request.latitude)}
                lng={Number(request.longitude)}
                key={i}
                onClick={() => onCardClicked(i)}
              >
                <Paper elevation={3} className={classes.paper}>
                  <h1 className="text-[1.75vh] font-bold text-center pb-1">
                    {' '}
                    {request.title}
                  </h1>
                  <img
                    className={`${classes.pointer} w-20 h-14 md:w-20 md:h-14 lg:w-20 lg:h-14 object-cover`}
                    src={
                      request.pictures && request.pictures[0]
                        ? `${process.env.REACT_APP_API_URL}/image/${request.pictures[0]}`
                        : noImage
                    }
                  />
                  {console.log(request.pictures)}
                  <h1 className="pt-1 text-center font-bold">
                    ${createRoundedRange(request.dollars)}
                  </h1>
                </Paper>
              </div>
            ))}
        </GoogleMap>
      ) : (
        <div className="flex items-center justify-center h-full">
          <CircularProgress />
        </div>
      )}
    </div>
  )
}

export default ProviderMap
