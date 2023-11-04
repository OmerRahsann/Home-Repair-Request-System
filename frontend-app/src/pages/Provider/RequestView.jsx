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
  const [requests, setRequests] = useState([])
  const [coords, setCoords] = useState({})
  const [bounds, setBounds] = useState(null)
  const [selectedCardIndex, setSelectedCardIndex] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [autocomplete, setAutocomplete] = useState(null)
  const [showNavBar, setShowNavBar] = useState(true)
  const [selectedLocation, setSelectedLocation] = useState(null)


  // Handle card click and store the index
  const handleCardClick = (index) => {
    setSelectedCardIndex(index)

    // You can also pass this index to other files/components here.
  }

  console.log(autocomplete)

  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
    
      ({ coords: { latitude, longitude } }) => {
        setIsLoading(true)
        
        setCoords({ lat: latitude, lng: longitude })
        const ne = {
          lat: latitude + .015,
          lng: longitude + .015,
        };
        
        const sw = {
          lat: latitude - .015,
          lng: longitude - .015,
        };
        setBounds({ ne, sw });
        setIsLoading(false)

      },
      
    )
  }, [])

  useEffect(() => {
    if (bounds) {
      setIsLoading(true)
      const { ne, sw } = bounds

      const url = `http://localhost:8080/api/provider/service_requests/nearby?latitudeSW=${sw.lat}&longitudeSW=${sw.lng}&latitudeNE=${ne.lat}&longitudeNE=${ne.lng}`

      // Make the GET request using Axios
      axios
        .get(url, { withCredentials: true })
        .then((response) => {
          console.log({ response })
          // Handle the response and set the serviceRequests state
          setRequests(response?.data)
        })
        .catch((error) => {
          console.error('Error:', error)
        })
        setIsLoading(false)
    }
  }, [bounds])

  const onRequestChanged = () => {
    const place = autocomplete.getPlace();
    const lat = place.geometry.location.lat();
    const lng = place.geometry.location.lng();

    setSelectedLocation({lat, lng})
  }

  const onLoad = (autoC) => setAutocomplete(autoC)

  return (
    <div>
      {showNavBar ? (
        <div className="flex flex-row">
          <button onClick={() => setShowNavBar(false)}>Hide</button>
          <NavBarProvider />
        </div>
      ) : (
        <button onClick={() => setShowNavBar(true)}>show</button>
      )}
      <div className="flex p-1 rounded-md flex-row justify-between bg-custom-grain p-3">
        <h1 className="font-bold text-lg p-2">Requests Near You!</h1>

        <Autocomplete onLoad={onLoad} onPlaceChanged={onRequestChanged}>
          <div className="flex flex-row bg-custom-gray rounded-md">
            <InputBase placeholder="Search by location..." className="p-1" />
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
            setBounds={setBounds}
            setCoords={setCoords}
            coords={coords}
            onCardClicked={handleCardClick}
            requests={requests}
            selectedLocation={selectedLocation}
          />
        </div>
      </div>
    </div>
  )
}
export default RequestView
