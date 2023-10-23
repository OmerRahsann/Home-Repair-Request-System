import { useState } from 'react'
import Map from '../../components/Map/Map'
import RequestList from '../../components/ServiceProviderHome/RequestList'
import NavBarProvider from '../../components/Navbar/NavBarProvider'

function RequestView() {
  const [viewPort, setViewPort] = useState({
    latitude: 45.2,
    longitude: -75,
    width: '100vw',
    height: '100vw',
    zoom: 10,
  })

  return (
    <div>
      <NavBarProvider />
      <div class="flex">
        <div class="w-1/3">
          <RequestList />
        </div>
        <div class="w-2/3">
          <Map />
        </div>
      </div>
    </div>
  )
}
export default RequestView
