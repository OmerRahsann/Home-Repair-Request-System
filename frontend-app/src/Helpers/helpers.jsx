import axios from 'axios'
export function createRoundedRange(value) {
  // Calculate the start of the rounded range
  const start = Math.ceil(value / 10) * 10 // Round up to the nearest 10th
  const adjustedStart = start - start * 0.2

  // Calculate the end of the range (e.g., 10 - 20)
  const end = Math.ceil(start + start * 0.3)

  // Construct the range string
  const rangeString = `${adjustedStart} - ${end}`

  return rangeString
}

export async function getServices() {
  try {
    const response = await axios.get(
      `${process.env.REACT_APP_API_URL}/api/provider/service_requests/services`,
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

export async function getNotifications() {
  const response = await axios.get(
    '/api/notifications',
    {
      withCredentials: true,
    },
  )
  return response.data
}

export async function markReadNotifications() {
  try {
    const response = await axios.post(
      `${process.env.REACT_APP_API_URL}/api/notifications/mark_read`,
      {
        withCredentials: true,
      },
    )
    return response.data
  } catch (error) {
    console.error('Error', error)
  }
}

export async function clearNotifications() {
  try {
    const response = await axios.post(
      `${process.env.REACT_APP_API_URL}/api/notifications/clear`,
      {
        withCredentials: true,
      },
    )
    return response.data
  } catch (error) {
    console.error('Error', error)
  }
}

// helpers.jsx

export function extractTownAndStateFromAddress(address) {
  const parts = address.split(',').map((part) => part.trim())
  console.log(parts)
  if (parts.length >= 3) {
    const town = parts[1]
    const state = parts[2]
    return `${town}, ${state}`
  } else {
    return { town: '', state: '' } // Return default values or handle the case where the address format is not as expected
  }
}

export const isValidEmail = (email) => {
  // Regular expression for basic email validation
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

export const formatPhoneNumber = (input) => {
  // Remove non-numeric characters from the input
  const phoneNumber = input.replace(/\D/g, '')

  // Truncate the input to the first 10 digits
  const truncatedPhoneNumber = phoneNumber.slice(0, 10)
  console.log(truncatedPhoneNumber)

  // Apply phone number formatting with parentheses and dashes
  if (truncatedPhoneNumber.length <= 3) {
    return `(${truncatedPhoneNumber}`
  } else if (truncatedPhoneNumber.length <= 6) {
    return `(${truncatedPhoneNumber.slice(0, 3)}) ${truncatedPhoneNumber.slice(
      3,
    )}`
  } else {
    return `(${truncatedPhoneNumber.slice(0, 3)}) ${truncatedPhoneNumber.slice(
      3,
      6,
    )}-${truncatedPhoneNumber.slice(6, 10)}`
  }
}
