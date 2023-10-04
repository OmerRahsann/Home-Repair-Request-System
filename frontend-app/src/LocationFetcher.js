import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

function LocationFetcher({ userId }) {
  const [location, setLocation] = useState(null);
  const [error, setError] = useState(null);
  const [askingPermission, setAskingPermission] = useState(false);
  const [storingLocation, setStoringLocation] = useState(false);
  const [processCompleted, setProcessCompleted] = useState(
    !!sessionStorage.getItem('locationShared')
  );

  
  const navigate = useNavigate();

  const storeLocation = async (location) => {
    setStoringLocation(true);
    
    const endpoint = `/api/v1/user/updateLocation/${userId}`;

    try {
      await axios.put(endpoint, {
        latitude: location.latitude,
        longitude: location.longitude
      });
    } catch (storeError) {
      console.error(`Failed to store location: ${storeError.message}`); // or display this error to the user if needed
    } finally {
      setStoringLocation(false);
      navigate('/auth'); // Redirect to login regardless of API success or failure
      setLocation(null);
      setProcessCompleted(true);
      sessionStorage.setItem('locationShared', 'true');
    }

};

  const handleFetchLocation = () => {
    setAskingPermission(true);

    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition(
        position => {
          const { latitude, longitude } = position.coords;
          setLocation({ latitude, longitude });
          storeLocation({ latitude, longitude });  // Store the location after fetching it
          setAskingPermission(false);
        },
        err => {
          setError(err.message);
          setAskingPermission(false);
        }
      );
    } else {
      setError("Geolocation is not supported by this browser.");
      setAskingPermission(false);
    }
  };

  if (askingPermission || storingLocation) {
    return <p>Processing...</p>;
  }

  if (!location) {
    return (
      <div>
        {error ? <p>Error: {error}</p> : null}
        <button onClick={handleFetchLocation}>
          Share your location
        </button>
      </div>
    );
  }

  // Not likely to be reached since after storing the location we're redirecting the user.
  if (!processCompleted && !location) {
    return (
      <div>
        {error ? <p>Error: {error}</p> : null}
        <button onClick={handleFetchLocation}>
          Share your location
        </button>
      </div>
    );
}
}

export default LocationFetcher;

