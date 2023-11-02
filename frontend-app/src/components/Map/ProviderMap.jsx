import React, { useState, useEffect } from 'react';
import GoogleMapReact from 'google-map-react';
import mapStyles from '../../mapStyles';
import useStyles from './styles.js';
import {
  Paper,
  Typography,
  useMediaQuery,
  CircularProgress,
} from '@material-ui/core';
import styles from './styles.js';

const ProviderMap = ({
  coords,
  requests,
  setCoords,
  setBounds,
  onCardClicked,
}) => {
  const matches = useMediaQuery('(min-width:600px)');
  const classes = useStyles();
  const [loading, setLoading] = useState(true);
  const [childClicked, setChildClicked] = useState(null);
  const [latLng, setLatLng] = useState(null);

  const reverseGeocode = async (address) => {
    try {
      const response = await fetch(
        `https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=${process.env.REACT_APP_GOOGLE_API_KEY}`,
      );

      if (response.ok) {
        const data = await response.json();
        if (data.results.length > 0) {
          const location = data.results[0].geometry.location;
          return location;
        } else {
          console.error('No results found for the given address.');
          return null;
        }
      } else {
        console.error('Geocoding request failed:', response.statusText);
        return null;
      }
    } catch (error) {
      console.error('Error during geocoding request:', error);
      return null;
    }
  };

  useEffect(() => {
    // Iterate through the requests to reverse geocode the addresses and update the state
    const geocodePromises = requests.map((request) => reverseGeocode(request.address));

    Promise.all(geocodePromises).then((locations) => {
      // Update the state with the locations
      setLatLng(locations);
      setLoading(false);
    });
  }, [requests]);

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
          setCoords({ lat: e.center.lat, lng: e.center.lng });
          setBounds({ ne: e.marginBounds.ne, sw: e.marginBounds.sw });
        }}
        onGoogleApiLoaded={() => setLoading(false)}
      >
        {requests.length !== 0 && requests.map((request, i) => (
          <div
            className={classes.markerContainer}
            lat={latLng && latLng[i] ? latLng[i].lat : Number(request.latitude)}
            lng={latLng && latLng[i] ? latLng[i].lng : Number(request.longitude)}
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
  );
};

export default ProviderMap;
