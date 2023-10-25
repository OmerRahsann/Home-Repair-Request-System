import React from 'react'
import { render, fireEvent, screen } from '@testing-library/react'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import RequestEdit from '../components/Customer/RequestEdit'

jest.mock('@react-google-maps/api', () => ({
  Autocomplete: ({ onLoad, onPlaceChanged }) => {
    // Mock the Autocomplete component
    return <div data-testid="autocomplete-mock"></div>
  },
}))

describe('ServiceRequestForm', () => {
  it('renders the form', () => {
    render(<RequestEdit />)

    expect(screen.getByText('Edit Your Service Request')).toBeInTheDocument()
    expect(screen.getByText('Project Title')).toBeInTheDocument()
    expect(screen.getByText('Project Description')).toBeInTheDocument()
    expect(screen.getByText('Project Location')).toBeInTheDocument()
    expect(screen.getByText('Project Category')).toBeInTheDocument()
    expect(
      screen.getByText('Upload Additional Project Pictures'),
    ).toBeInTheDocument()
    expect(screen.getByText('Desired Price')).toBeInTheDocument()
    expect(screen.getByText('SAVE CHANGES')).toBeInTheDocument()
  })

  it('submits the form', async () => {
    render(<RequestEdit />)

    // Fill in form inputs
    fireEvent.change(screen.getByPlaceholderText('ex: Gutter Cleanup'), {
      target: { value: 'Test Title' },
    })
    fireEvent.change(screen.getByPlaceholderText('ex: 250'), {
      target: { value: '100' },
    })

    // Submit the form
    fireEvent.click(screen.getByText('SAVE CHANGES'))
  })
})
