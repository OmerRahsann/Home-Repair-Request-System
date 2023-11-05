import axios from 'axios'
export function createRoundedRange(value) {
  // Calculate the start of the rounded range
  const start = Math.ceil(value / 10) * 10 // Round up to the nearest 10th

  // Calculate the end of the range (e.g., 10 - 20)
  const end = Math.ceil(start + start * 0.3)

  // Construct the range string
  const rangeString = `${start} - ${end}`

  return rangeString
}

export async function getServices() {
  try {
    const response = await axios.get(
      'http://localhost:8080/api/provider/service_requests/services',
      {
        withCredentials: true,
      },
    )

    // Extract the services from the response
    const servicesData = response.data

    // Transform the servicesData into the desired format (label and value are the same)
    const transformedServices = servicesData.map((service) => ({
      label: service,
      value: service,
    }))

    return transformedServices // Return the transformed services
  } catch (error) {
    console.error('Error:', error)
    throw error // Re-throw the error for handling in the calling code
  }
}
