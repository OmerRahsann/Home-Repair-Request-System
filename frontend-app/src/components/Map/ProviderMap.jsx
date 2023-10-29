import React from 'react';
import GoogleMapReact from 'google-map-react';
import mapStyles from "../../mapStyles";
import { Marker } from '@react-google-maps/api';
import useStyles from './styles.js';
import { Paper, Typography, useMediaQuery } from '@material-ui/core';
import { useState, useEffect } from 'react';

const ProviderMap = ({ coords, requests, setCoords, setChildClicked, setBounds }) => {
  const matches = useMediaQuery('(min-width:600px)');
  const classes = useStyles();
  const [mapIsBeingDragged, setMapIsBeingDragged] = useState(false);

  const handleMapChange = (e) => {
    if (mapIsBeingDragged) {
      // Map is being dragged, hide the requests data
      setMapIsBeingDragged(false);
    } else {
      setCoords({ lat: e.center.lat, lng: e.center.lng });
    }
  };

  

  return (
    <div className="h-[85vh] w-100%">
      <GoogleMapReact
        bootstrapURLKeys={{ key: process.env.REACT_APP_GOOGLE_API_KEY }}
        center={coords}
        defaultZoom={14}
        options={{ disableDefaultUI: true, zoomControl: true, styles: mapStyles }}
        margin={[50, 50, 50, 50]}
        onChange={(e) => { 
          setCoords({ lat: e.center.lat, lng: e.center.lng });
          setBounds({ ne: e.marginBounds.ne, sw: e.marginBounds.sw });
         
        }}
        onChildClick={(child) => setChildClicked(child)}
      >
        {requests.length && requests.map((request, i) => (
          <div
            className="transform translate-z-0 -translate-x-1/2 -translate-y-full"
            lat={Number(request.latitude)}
            lng={Number(request.longitude)}
            key={i}
          >
            {!matches ? <text></text> : (
              <Paper elevation={3} className={classes.paper}>
                <Typography className={classes.typography} variant="subtitle2" gutterBottom> {request.name}</Typography>
                <img
                  className={classes.pointer}
                  src={request.photo ? request.photo.images.large.url : 'https://www.foodserviceandhospitality.com/wp-content/uploads/2016/09/Restaurant-Placeholder-001.jpg'}
                />
              </Paper>
            )}
          </div>
        ))}
      </GoogleMapReact>
    </div>
  );
};

export default ProviderMap;
