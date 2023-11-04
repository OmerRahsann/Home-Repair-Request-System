import { useState, useEffect } from 'react'
import RequestList from '../../components/ServiceProviderHome/RequestList'
import NavBarProvider from '../../components/Navbar/NavBarProvider'
import ProviderMap from '../../components/Map/ProviderMap'
import axios from 'axios'

function RequestView() {
  const [requests, setRequests] = useState([])
  const [coords, setCoords] = useState({})
  const [bounds, setBounds] = useState(null)
  const [selectedCardIndex, setSelectedCardIndex] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [autocomplete, setAutocomplete] = useState(null)
  const [selectedLocation, setSelectedLocation] = useState(null)

  // Handle card click and store the index
  const handleCardClick = (index) => {
    setSelectedCardIndex(index)
  }

  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      ({ coords: { latitude, longitude } }) => {
        setIsLoading(true)

        setCoords({ lat: latitude, lng: longitude })
        const ne = {
          lat: latitude + 0.015,
          lng: longitude + 0.015,
        }

        const sw = {
          lat: latitude - 0.015,
          lng: longitude - 0.015,
        }
        setBounds({ ne, sw })
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
    const place = autocomplete.getPlace()
    const lat = place.geometry.location.lat()
    const lng = place.geometry.location.lng()

    setSelectedLocation({ lat, lng })
  }

  const onLoad = (autoC) => setAutocomplete(autoC)

  return (
    <div>
      <NavBarProvider />

      <div className="flex">
        <div className="w-1/3">
          <RequestList
            requests={requests}
            selectedCardIndex={selectedCardIndex}
            isLoading={isLoading}
            onLoad={onLoad}
            onRequestChanged={onRequestChanged}
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
