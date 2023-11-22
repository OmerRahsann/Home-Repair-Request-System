import { useState, useEffect } from 'react'
import RequestList from '../../components/ServiceProviderHome/RequestList'
import NavBarProvider from '../../components/Navbar/NavBarProvider'
import ProviderMap from '../../components/Map/ProviderMap'
import axios from 'axios'
import { checkIsServiceProviderLoggedIn } from '../../AuthContext'
import { useNavigate } from 'react-router-dom'

function RequestView() {
  const [requests, setRequests] = useState([])
  const [coords, setCoords] = useState({})
  const [bounds, setBounds] = useState(null)
  const [selectedCardIndex, setSelectedCardIndex] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [autocomplete, setAutocomplete] = useState(null)
  const [selectedLocation, setSelectedLocation] = useState(null)
  const [categoryChange, setCategoryChange] = useState(null)
  const [priceRangeChange, setPriceRangeChange] = useState(null)
  const navigate = useNavigate()

  const handleCardClick = (index) => {
    setSelectedCardIndex(index)
  }

  const [loggedIn, setLoggedIn] = useState(false)
  const [hasSecondEffectRun, setHasSecondEffectRun] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const isLoggedIn = await checkIsServiceProviderLoggedIn()
        setLoggedIn(isLoggedIn)
        console.log(isLoggedIn)
        if (!isLoggedIn) {
          navigate('/provider/login')
          alert(
            'You do not have access to this page. Please create a Service Provider Account to access this page.',
          )
        }
      } catch (error) {
        console.error('Error checking if customer is logged in:', error)
      } finally {
        setHasSecondEffectRun(true)
      }
    }

    fetchData()
  }, [])

  useEffect(() => {
    if (navigator.geolocation) {
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
    }
  }, [hasSecondEffectRun])

  useEffect(() => {
    if (bounds) {
      setIsLoading(true)
      const { ne, sw } = bounds

      let url = `${process.env.REACT_APP_API_URL}/api/provider/service_requests/nearby?latitudeS=${sw.lat}&longitudeW=${sw.lng}&latitudeN=${ne.lat}&longitudeE=${ne.lng}`
      if (categoryChange) {
        url += `&serviceType=${categoryChange.value}`
      }

      // Add price range parameters if selected
      if (priceRangeChange) {
        url += `&lowerDollarRange=${priceRangeChange.value[0]}&higherDollarRange=${priceRangeChange.value[1]}`
      }

      console.log(url)

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
  }, [bounds, categoryChange, priceRangeChange])

  const onRequestChanged = () => {
    if (autocomplete) {
      const place = autocomplete.getPlace()
      if (place && place.geometry && place.formatted_address) {
        const lat = place.geometry.location.lat()
        const lng = place.geometry.location.lng()
        setSelectedLocation({ lat, lng })
      } else {
        window.alert('Please Enter a Valid Address.')
      }
    }
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
            setCategoryChange={setCategoryChange}
            setPriceRangeChange={setPriceRangeChange}
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
