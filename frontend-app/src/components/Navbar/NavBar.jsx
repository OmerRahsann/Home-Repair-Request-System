import { Fragment, useState } from 'react'
import { Disclosure, Menu, Transition } from '@headlessui/react'
import { Bars3Icon, XMarkIcon, BellIcon } from '@heroicons/react/24/outline'
import { UserCircleIcon } from '@heroicons/react/24/outline'
import { useLocation } from 'react-router-dom'
import logo from '../../Logos/mainLogo.png'
import { logout } from '../../AuthContext'
import Notifications from '../Notifications/Notifications'
import NotificationSlider from '../Notifications/NotificationSlider'

const navigation = [
  { name: 'Home', href: '/', current: true },
  { name: 'My Requests', href: '/customer/myrequests', current: false },
  { name: 'My Appointments', href: '/customer/myappointments', current: false },
]

function classNames(...classes) {
  return classes.filter(Boolean).join(' ')
}

export default function NavBar({ isLoggedIn }) {
  const location = useLocation()
  const [notifications, setNotifications] = useState(false)

  const closeNotifications = () => {
    setNotifications(false)
  }

  // Update the current property in the navigation array based on the current location.
  navigation.forEach((item) => {
    item.current = item.href === location.pathname
  })

  return (
    <Disclosure as="nav">
      {({ open }) => (
        <>
          <div className="pt-2 bg-gray-100 p-4">
            <div className="relative flex h-24 items-center justify-between p-2">
              <div className="absolute inset-y-0 left-0 flex items-center sm:hidden">
                {/* Mobile menu button*/}
                <Disclosure.Button className="relative inline-flex items-center justify-center rounded-md p-2 text-gray-400 hover:bg-gray-700 hover:text-white focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white">
                  <span className="absolute -inset-0.5" />
                  <span className="sr-only">Open main menu</span>
                  {open ? (
                    <XMarkIcon className="block h-6 w-6" aria-hidden="true" />
                  ) : (
                    <Bars3Icon className="block h-6 w-6" aria-hidden="true" />
                  )}
                </Disclosure.Button>
              </div>
              <div className="flex flex-1 items-center justify-center  sm:justify-start">
                <div className="flex flex-shrink-0 items-center">
                  <img className="h-36" src={logo} alt="RepairRadar" />
                </div>
                <div className="hidden sm:ml-4 sm:block">
                  <div className="flex space-x-4">
                    {navigation.map((item) => (
                      <a
                        key={item.name}
                        href={item.href}
                        className={classNames(
                          item.current
                            ? 'bg-custom-black text-white'
                            : 'text-black hover:bg-custom-grain hover:text-white',
                          'rounded-md px-3 py-2 text-[2vh] font-bold',
                        )}
                        aria-current={item.current ? 'page' : undefined}
                      >
                        {item.name}
                      </a>
                    ))}
                  </div>
                </div>
              </div>

              <div className="absolute inset-y-0 right-4 flex items-center space-x-2 pr-2 sm:static sm:inset-auto sm:ml-6 sm:pr-2">
                {!isLoggedIn ? (
                  <>
                    <a
                      href="/provider/login"
                      className="text-custom-black hover:bg-custom-grain hover:text-white flex-shrink-0 hidden sm:block"
                    >
                      Become a Service Provider!
                    </a>
                    <a
                      href={'/customer/login'}
                      className="bg-custom-black text-white hover:bg-custom-grain hover:text-white rounded-md px-3 py-2 text-[2vh] font-bold"
                    >
                      Sign In
                    </a>{' '}
                  </>
                ) : null}

                {/* Profile dropdown */}
                {isLoggedIn ? (
                  <Menu as="div" className="relative ml-3">
                    <div className="flex flex-row">
                      <button
                        onClick={() => setNotifications(!notifications)}
                        type="button"
                        className="relative rounded-full p-1 text-custom-maroon  focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-800"
                      >
                        <span className="absolute -inset-1.5" />
                        <span className="sr-only">View notifications</span>
                        <BellIcon className="h-6 w-6" aria-hidden="true" />
                      </button>
                      <div className="p-2"></div>
                      <Menu.Button className="bg-custom-maroon text-sm p-2 focus:outline-none focus:ring-1 focus:ring-white focus:ring-offset-1 focus:ring-offset-gray-800">
                        <UserCircleIcon className="h-6 w-6 rounded-full bg-white text-custom-maroon" />
                      </Menu.Button>
                    </div>
                    <Transition
                      as={Fragment}
                      enter="transition ease-out duration-100"
                      enterFrom="transform opacity-0 scale-95"
                      enterTo="transform opacity-100 scale-100"
                      leave="transition ease-in duration-75"
                      leaveFrom="transform opacity-100 scale-100"
                      leaveTo="transform opacity-0 scale-95"
                    >
                      <Menu.Items className="absolute right-0 z-10 mt-2 w-48 origin-top-right rounded-md bg-white py-1 shadow-lg ring-1 ring-black ring-opacity-5 focus:outline-none">
                        <Menu.Item>
                          {({ active }) => (
                            <a
                              href="/customer/myprofile"
                              className={classNames(
                                active ? 'bg-gray-100' : '',
                                'block px-4 py-2 text-sm text-gray-700',
                              )}
                            >
                              Your Profile
                            </a>
                          )}
                        </Menu.Item>
                        <Menu.Item>
                          {({ active }) => (
                            <a
                              onClick={logout}
                              href="/"
                              className={classNames(
                                active ? 'bg-gray-100' : '',
                                'block px-4 py-2 text-sm text-gray-700',
                              )}
                            >
                              Sign out
                            </a>
                          )}
                        </Menu.Item>
                      </Menu.Items>
                    </Transition>
                  </Menu>
                ) : null}
              </div>
            </div>
          </div>
          {notifications && (
            <div
              className={`fixed top-5 right-5 bg-custom-grain p-4 rounded-md z-20 ${
                notifications
                  ? 'opacity-100 transform translate-x-0'
                  : 'opacity-0 transform translate-x-full'
              } transition-all duration-500 ease-in-out`}
            >
              <Notifications onClose={closeNotifications} customer={true} />
            </div>
          )}

          <NotificationSlider />

          <Disclosure.Panel className="sm:hidden">
            <div className="space-y-1 px-2 pb-3 pt-2">
              {navigation.map((item) => (
                <Disclosure.Button
                  key={item.name}
                  as="a"
                  href={item.href}
                  className={classNames(
                    item.current
                      ? 'bg-gray-900 text-white'
                      : 'text-gray-300 hover:bg-gray-700 hover:text-white',
                    'block rounded-md px-3 py-2 text-base font-medium',
                  )}
                  aria-current={item.current ? 'page' : undefined}
                >
                  {item.name}
                </Disclosure.Button>
              ))}
            </div>
          </Disclosure.Panel>
        </>
      )}
    </Disclosure>
  )
}
