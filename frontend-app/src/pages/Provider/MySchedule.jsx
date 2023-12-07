import React from 'react'
import NavBarProvider from '../../components/Navbar/NavBarProvider'
import ProviderCalendar from 'components/ServiceProviderHome/ProviderCalendar'

function MyJobs() {
  return (
    <div>
      <NavBarProvider />

      <ProviderCalendar createEventPopop={false} />
    </div>
  )
}
export default MyJobs
