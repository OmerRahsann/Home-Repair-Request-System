import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import ServiceRequestCard from '../components/ServiceProviderHome/ServiceRequestCard';

const request = {
  title: 'Sample Request',
  service: 'Yardwork',
  dollars: '200 - 350',
  pictures: [], // Add sample data for pictures
  address: 'Sample Adress, Glassboro, NJ 08028'
};

describe('ServiceRequestCard', () => {
  it('renders the request card with correct details', () => {
    render(<ServiceRequestCard request={request} />);

    expect(screen.getByText('Sample Request')).toBeInTheDocument();
    expect(screen.getByText('Yardwork')).toBeInTheDocument();
    // Add more specific assertions based on your component structure.
  });

  it('handles the "Open Job" button click', () => {
    render(<ServiceRequestCard request={request} />);

    const openJobButton = screen.getByText('Open Job');
    fireEvent.click(openJobButton);

    // Add assertions for the expected behavior when the button is clicked.
  });

});
