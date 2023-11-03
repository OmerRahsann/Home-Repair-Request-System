import React from 'react'
import { render, screen } from '@testing-library/react'
import PasswordStrength from 'components/PasswordStrength'

describe('PasswordStrength', () => {
  it("doesn't render anything with empty passwords", () => {
    let satisfied;
    let setSatisfied = (val) => satisfied = val;
    render(<PasswordStrength password="" confirmPassword="" updateSatisfied={setSatisfied} />)
    expect(document.body.querySelector("p")).toBe(null)
    expect(satisfied).toBe(false)
  })
  it("renders password requirements when password is filled", () => {
    let satisfied;
    let setSatisfied = (val) => satisfied = val;
    render(<PasswordStrength password="A" confirmPassword="" updateSatisfied={setSatisfied} />)
    expect(satisfied).toBe(false)
    expect(screen.queryByText("match")).not.toBeInTheDocument()
    expect(screen.getByText("✗ Password must be 8 to 64 characters")).toBeInTheDocument()
    expect(screen.getByText("✓ Password must have at least 1 uppercase letter")).toBeInTheDocument()
    expect(screen.getByText("✗ Password must have at least 1 number")).toBeInTheDocument()
  })
  it("checks whether password and confirmPassword match", () => {
    let satisfied;
    let setSatisfied = (val) => satisfied = val;
    render(<PasswordStrength password="A" confirmPassword="B" updateSatisfied={setSatisfied} />)
    expect(satisfied).toBe(false)
    expect(screen.getByText("✗ Passwords do not match")).toBeInTheDocument()
    expect(screen.getByText("✗ Password must be 8 to 64 characters")).toBeInTheDocument()
    expect(screen.getByText("✓ Password must have at least 1 uppercase letter")).toBeInTheDocument()
    expect(screen.getByText("✗ Password must have at least 1 number")).toBeInTheDocument()
  })
  it("is satisifed when given a good password", () => {
    let satisfied;
    let setSatisfied = (val) => satisfied = val;
    render(<PasswordStrength password="Example1" confirmPassword="Example1" updateSatisfied={setSatisfied} />)
    expect(satisfied).toBe(true)
    expect(screen.getByText("✓ Passwords match")).toBeInTheDocument()
    expect(screen.getByText("✓ Password must be 8 to 64 characters")).toBeInTheDocument()
    expect(screen.getByText("✓ Password must have at least 1 uppercase letter")).toBeInTheDocument()
    expect(screen.getByText("✓ Password must have at least 1 number")).toBeInTheDocument()
  })
})
