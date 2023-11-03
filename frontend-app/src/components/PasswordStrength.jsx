const passwordRules = [];
const addPasswordRule = (label, predicate) => passwordRules.push({label, predicate})
addPasswordRule("Password must be 8 to 64 characters", (password) => 8 <= password.length && password.length <= 64 && password.search(/\S/) != -1)
addPasswordRule("Password must have at least 1 uppercase letter", (password) => password.search(/[A-Z]/) != -1)
addPasswordRule("Password must have at least 1 number", (password) => password.search(/[0-9]/) != -1)

function checkPasswordRequirements(password) {
  let rows = [];
  for (const rule of passwordRules) {
    let satisfied = rule.predicate(password);
    rows.push(
      <p
        className={
          satisfied ? 'text-green-500' : 'text-red-500'
        }
      >
        {(satisfied ? '✓ ' : '✗ ') + rule.label}
      </p>
    )
  }
  return rows
}

function checkSatisfied(password, confirmPassword) {
  for (const rule of passwordRules) {
    if (!rule.predicate(password)) {
      return false
    }
  } 
  return password === confirmPassword
}

export default function PasswordStrength({ password, confirmPassword, updateSatisfied }) {
  const passwordsMatch = password === confirmPassword
  updateSatisfied(checkSatisfied(password, confirmPassword))

  return (
    <div>
      {confirmPassword != '' && (
        <p
          className={
            passwordsMatch ? 'text-green-500' : 'text-red-500'
          }
        >
          {passwordsMatch
            ? '✓ Passwords match'
            : '✗ Passwords do not match'}
        </p>
      )}
      {password != '' && checkPasswordRequirements(password)}
    </div>
  )
}