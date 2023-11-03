import React from 'react'
import ImageSlider from '../ImageSlider'
import { Fragment } from 'react'
import RequestDetails from '../Customer/RequestDetails'

function ServiceRequestCard({ request, selected, refProp }) {
  if (selected)
    refProp?.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  return (
    <Fragment>
      <RequestDetails request={request} />
    </Fragment>
  )
}

export default ServiceRequestCard
