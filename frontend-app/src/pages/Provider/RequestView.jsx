import { useState, useEffect } from 'react'
import RequestList from '../../components/ServiceProviderHome/RequestList'
import NavBarProvider from '../../components/Navbar/NavBarProvider'
import ProviderMap from '../../components/Map/ProviderMap'

function RequestView() {
  const [viewPort, setViewPort] = useState({
    latitude: 45.2,
    longitude: -75,
    width: '100vw',
    height: '100vw',
    zoom: 10,
  })

  const fakeRequests = [
    {
      latitude: 39.7213696,
      longitude: -75.1140864,
      name: "Sample Place 1",
      rating: 4.5,
    },
    {
      latitude: 39.7313655,
      longitude: -75.1140833,
      name: "Sample Place 2",
      rating: 4.5,
    },
    // Add more fake data as needed
  ];

  const [coords, setCoords] = useState({});
  const[childClick, setChildClick] = useState(null)
  const [bounds, setBounds] = useState(null);
  useEffect(() => {
    navigator.geolocation.getCurrentPosition(({ coords: { latitude, longitude } }) => {
      setCoords({ lat: latitude, lng: longitude });
      console.log(latitude + " " + longitude)
    });
  }, []);



  return (
    <div>
      <div className="flex">
        <div className="w-1/3">
          <RequestList />
        </div>
        <div className="w-2/3">
          <ProviderMap requests={fakeRequests }  
            setBounds={setBounds}
            setCoords={setCoords}
            coords={coords}
            setChildClicked={setChildClick}/>
        </div>
      </div>
    </div>
  )
}
export default RequestView
