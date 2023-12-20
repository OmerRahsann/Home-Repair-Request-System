import React from 'react'
import { Autocomplete } from '@react-google-maps/api'
import { AppBar, Toolbar, Typography, InputBase, Box } from '@material-ui/core'

const SearchBar = ({ onRequestChanged, onLoad }) => {
  return (
    <AppBar position="static">
      <Toolbar className>
        <Box display="flex">
          <Autocomplete onLoad={onLoad} onPlaceChanged={onRequestChanged}>
            <div className>
              <div className></div>
              <InputBase placeholder="Search…" />
            </div>
          </Autocomplete>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default SearchBar
