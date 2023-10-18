import React from 'react';
import GoogleMapReact from "google-map-react"
//import { Paper, Typography, useMediaQuery } from '@material-ui/core';




const Map = ({ places, setCoords, setBounds, setChildClicked, weatherData }) => {
  //const matches = useMediaQuery('(min-width:600px)');
  //const classes = useStyles();
  const coords = {lat: 0, lng: 0}

  return (
    <div className='h-[85vh] w-100%'>
      <GoogleMapReact
        bootstrapURLKeys={{ key: 'AIzaSyB-Hir-BFLaHrDngWHU5dXi3wA4VfIshs4' }}
        defaultCenter={coords}
        center={coords}
        defaultZoom={14}
        margin={[50, 50, 50, 50]}
        options={''}
       
      >
      </GoogleMapReact>
    </div>
  );
};

export default Map;