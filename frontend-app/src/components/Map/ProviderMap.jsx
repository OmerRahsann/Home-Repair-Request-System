import React, { useState, useEffect } from 'react'
import GoogleMapReact from 'google-map-react'
import mapStyles from '../../mapStyles'
import useStyles from './styles.js'
import {
  Paper,
  Typography,
  useMediaQuery,
  CircularProgress,
} from '@material-ui/core'
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
  const [loading, setLoading] = useState(false)
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
          setCoords({ lat: e.center.lat, lng: e.center.lng })
          setBounds({ ne: e.marginBounds.ne, sw: e.marginBounds.sw })
        }}
        onGoogleApiLoaded={() => setLoading(false)}
      >
        {requests?.map((request, i) => (
          <div
            lat={request.latitude}
            lng={request.longitude}
            key={i}
            onClick={() => onCardClicked(i)}
          >
            <Paper elevation={3} className={classes.paper}>
              <Typography
                className={classes.typography}
                variant="subtitle2"
                gutterBottom
              >
                {request.title}
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
