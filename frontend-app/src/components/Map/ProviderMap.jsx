import React from 'react'
import GoogleMapReact from 'google-map-react'
import mapStyles from '../../mapStyles'
import useStyles from './styles.js'
import {
  Paper,
  Typography,
  useMediaQuery,
  CircularProgress,
} from '@material-ui/core'
import { useState, useEffect } from 'react'
import styles from './styles.js'

const ProviderMap = ({
  coords,
  requests,
  setCoords,
  setBounds,
  onCardClicked,
}) => {
  const matches = useMediaQuery('(min-width:600px)')
  const classes = useStyles()
  const [loading, setLoading] = useState(true)
  const [childClicked, setChildClicked] = useState(null)

  return (
    <div className={classes.mapContainer}>
      <GoogleMapReact
        bootstrapURLKeys={{ key: process.env.REACT_APP_GOOGLE_API_KEY }}
        defaultCenter={coords}
        center={coords}
        defaultZoom={13}
        options={{
          disableDefaultUI: true,
          zoomControl: true,
          styles: mapStyles,
        }}
        margin={[50, 50, 50, 50]}
        onChange={(e) => {
          console.log(e)
          setCoords({ lat: e.center.lat, lng: e.center.lng })
          setBounds({ ne: e.marginBounds.ne, sw: e.marginBounds.sw })
        }}
        onGoogleApiLoaded={() => setLoading(false)}
      >
        {requests.length != 0 && requests.map((request, i) => (
          <div
            className={classes.markerContainer}
            lat={Number(request.latitude)}
            lng={Number(request.longitude)}
            key={i}
            onClick={() => onCardClicked(i)}
          >
            <Paper elevation={3} className={classes.paper}>
              <Typography
                className={classes.typography}
                variant="subtitle2"
                gutterBottom
              >
                {' '}
                {request.title}{' '}
              </Typography>
              <img
                className={classes.pointer}
                src={
                  request.photo
                    ? request.photo.images.large.url
                    : 'https://www.foodserviceandhospitality.com/wp-content/uploads/2016/09/Restaurant-Placeholder-001.jpg'
                }
              />
            </Paper>
          </div>
        ))}
      </GoogleMapReact>
    </div>
  )
}

export default ProviderMap
