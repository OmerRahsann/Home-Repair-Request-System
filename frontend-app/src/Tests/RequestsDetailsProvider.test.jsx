import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import RequestDetailsProvider from '../components/ServiceProviderHome/RequestDetailsProvider';

const request = {
  title: 'Sample Request',
  creationDate: new Date(),
  service: 'Sample Service',
  dollars: 100,
  description: 'Sample Description',
  status: 'Pending',
  address: 'Sample Address, Glassboro, NJ 08028',
  pictures: [],
};

describe('RequestDetailsProvider', () => {
  it('renders component with request details', () => {
    render(<RequestDetailsProvider request={request} />);
    
    // Assertions
    expect(screen.getByText('Sample Request')).toBeInTheDocument();
    expect(screen.getByText('Creation Date:')).toBeInTheDocument();
    expect(screen.getByText('Service Category:')).toBeInTheDocument();
    expect(screen.getByText('Desired Price Range:')).toBeInTheDocument();
    expect(screen.getByText('Description:')).toBeInTheDocument();
    expect(screen.getByText('Status:')).toBeInTheDocument();
   
  });

  it('opens modal when clicked', () => {
    render(<RequestDetailsProvider request={request} />);
    
    // Simulate a click event to open the modal
    fireEvent.click(screen.getByText('Sample Request'));
    
  });
});
