import React, { useState, useEffect } from 'react'
import mapStyles from '../../mapStyles'
import GoogleMap from 'google-maps-react-markers'
import useStyles from './styles.js'
import noImage from '../../Pictures/noImage.jpeg'
import { Paper, useMediaQuery, CircularProgress } from '@material-ui/core'
import { createRoundedRange } from '../../Helpers/helpers'

const ProviderMap = ({
  coords,
  defaultCoords,
  requests,
  setBounds,
  onCardClicked,
  selectedCardIndex,
}) => {
  const matches = useMediaQuery('(min-width:600px)')
  const classes = useStyles()
  const [map, setMap] = useState(null)

  const handleMapChange = ({ bounds }) => {
    const newBounds = {
      ne: {
        lat: bounds.getNorthEast().lat(),
        lng: bounds.getNorthEast().lng(),
      },
      sw: {
        lat: bounds.getSouthWest().lat(),
        lng: bounds.getSouthWest().lng(),
      },
    }

    setBounds(newBounds)
  }

  const handleGoogleApiLoaded = ({ map }) => {
    setMap(map)
  }

  useEffect(() => {
    if (map) {
      map.panTo(coords)
    }
  }, [map, coords])

  const loadingContent = (
    <div className="flex items-center justify-center h-full">
      <CircularProgress />
    </div>
  )

  return (
    <div className={classes.mapContainer}>
      <GoogleMap
        apiKey={process.env.REACT_APP_GOOGLE_API_KEY}
        defaultCenter={defaultCoords}
        defaultZoom={13}
        options={{
          disableDefaultUI: true,
          zoomControl: true,
          styles: mapStyles,
          gestureHandling: 'greedy',
        }}
        margin={[50, 50, 50, 50]}
        onChange={handleMapChange}
        onGoogleApiLoaded={handleGoogleApiLoaded}
        loadingContent={loadingContent}
      >
        {requests.length &&
          requests.map((request, i) => (
            <div
              className={classes.markerContainer}
              lat={Number(request.latitude)}
              lng={Number(request.longitude)}
              key={i}
              onClick={() => onCardClicked(i)}
              zIndex={i == selectedCardIndex ? 10 : 0}
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
                      ? `/image/${request.pictures[0]}`
                      : noImage
                  }
                />
                <h1 className="pt-1 text-center font-bold">
                  ${createRoundedRange(request.dollars)}
                </h1>
              </Paper>
            </div>
          ))}
      </GoogleMap>
    </div>
  )
}

export default ProviderMap
