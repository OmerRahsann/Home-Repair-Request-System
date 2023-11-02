import { useState, useEffect } from 'react'
import RequestList from '../../components/ServiceProviderHome/RequestList'
import NavBarProvider from '../../components/Navbar/NavBarProvider'
import ProviderMap from '../../components/Map/ProviderMap'
import SearchBar from '../../components/ServiceProviderHome/SearchBar'
import axios from 'axios'
import { Autocomplete } from '@react-google-maps/api'
import { InputBase } from '@material-ui/core'
import { FaSearch } from 'react-icons/fa'

function RequestView() {
  const fakeRequests = [
    {
      latitude: 39.7213696,
      longitude: -75.1140864,
      title: 'Sample Place 1',
      rating: 4.5,
    },
    {
      latitude: 39.7313655,
      longitude: -75.1140833,
      title: 'Sample Place 2',
      rating: 4.5,
    },
    // Add more fake data as needed
  ]

  const [requests, setRequests] = useState([])

  const [coords, setCoords] = useState({})
  const [bounds, setBounds] = useState({})
  const [selectedCardIndex, setSelectedCardIndex] = useState(0)
  const [isLoading, setIsLoading] = useState(false)
  const [autocomplete, setAutocomplete] = useState(null);

  // Handle card click and store the index
  const handleCardClick = (index) => {
    setSelectedCardIndex(index)

    // You can also pass this index to other files/components here.
  }
 
  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      ({ coords: { latitude, longitude } }) => {
        setCoords({ lat: latitude, lng: longitude })
        console.log(latitude + ' ' + longitude)
      },
    )
  }, [])

  useEffect(() => {
    if (bounds) {
      setIsLoading(true)
      // Wait for geolocation to complete
      const url = `http://localhost:8080/api/provider/service_requests/all`

      // Make the GET request using Axios
      axios
        .get(url, { withCredentials: true })
        .then((response) => {
          console.log({response})
          // Handle the response and set the serviceRequests state
          setRequests(response.data)
          setIsLoading(false)
        })
        .catch((error) => {
          console.error('Error:', error)
        })
      console.log(coords, bounds)
      // getRequestData(bounds.sw, bounds.ne)
      // Make GET Call here
    }
  }, [bounds,coords])

  const onRequestChanged = () => {
    const lat = autocomplete.getPlace().geometry.location.lat();
    const lng = autocomplete.getPlace().geometry.location.lng();

    setCoords({ lat, lng });
  };

  const onLoad = (autoC) => setAutocomplete(autoC);


  return (
    <div>
      <NavBarProvider />
      <div className="flex p-1 rounded-md flex-row justify-between bg-custom-grain p-3">
        
        <h1 className="font-bold text-lg p-2">
        Requests Near You!
      </h1>
      
      <Autocomplete
                      onLoad={onLoad}
                      onPlaceChanged={onRequestChanged}
                    >
                      <div className='flex flex-row bg-custom-gray rounded-md' >
                      <InputBase placeholder="Search by location..." className='p-1' />

                      </div>
                      
                    </Autocomplete>
                    
                  </div>
          
      <div className="flex">
        <div className="w-1/3">
          <RequestList
            requests={requests}
            selectedCardIndex={selectedCardIndex}
            isLoading={isLoading}
          />
        </div>
        <div className="w-2/3">
          <ProviderMap
            requests={requests}
            setBounds={setBounds}
            setCoords={setCoords}
            coords={coords}
            onCardClicked={handleCardClick}
          />
        </div>
      </div>
    </div>
  )
}
export default RequestView
